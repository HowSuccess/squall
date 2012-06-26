
package visitors.jsql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import optimizers.cost.NameTranslator;
import schema.ColumnNameType;
import util.ParserUtil;

/*
 * This class return a list of (sub)expressions which corresponds to visited expressions
 *   AND can be built out of inputTupleSchema
 * For example, if visited expression is LINEITEM.EXTENDEDPRICE * (1.0 - LINEITEM.DISCOUNT)
 *   and input schema is (LINEITEM.EXTENDEDPRICE, 1.0 - LINEITEM.DISCOUNT)
 *   _exprList will constis of a single expression LINEITEM.EXTENDEDPRICE * (1.0 - LINEITEM.DISCOUNT)
 * Used in ProjSchemaCreator
 */
public class SubExpressionVisitor implements ExpressionVisitor, ItemsListVisitor{
    private NameTranslator _nt;
    private List<ColumnNameType> _inputTupleSchema;
    private List<Expression> _exprList =  new ArrayList<Expression>();
    
    public SubExpressionVisitor(NameTranslator nt, List<ColumnNameType> inputTupleSchema){
        _nt = nt;
        _inputTupleSchema = inputTupleSchema;
    }
    
    public List<Expression> getExprs(){
        return _exprList;
    }
    
    public void visit(List<Expression> inputExprList){
        for(Expression expr: inputExprList){
            expr.accept(this);
        }
    }
        
    @Override
    public void visit(Column column) {
        //no subexpression recognized, still we add only mine columns
        String strExpr = ParserUtil.getStringExpr(column);
        if(_nt.contains(_inputTupleSchema, strExpr)){
            _exprList.add(column);
        }
    }    

    @Override
    public void visit(DoubleValue dv) {
        //if not recognized as a part of a subexpression, ignore it
    }

    @Override
    public void visit(LongValue lv) {
        //if not recognized as a part of a subexpression, ignore it
    }

    @Override
    public void visit(DateValue dv) {
        //if not recognized as a part of a subexpression, ignore it
    }
    
    @Override
    public void visit(StringValue sv) {
        //if not recognized as a part of a subexpression, ignore it
    }    
    
    @Override
    public void visit(Function fnctn) {
        if(!isRecognized(fnctn)){
            visit(fnctn.getParameters());
        }
    }
    
    @Override
    public void visit(ExpressionList el) {
        for (Iterator iter = el.getExpressions().iterator(); iter.hasNext();) {
            Expression expression = (Expression) iter.next();
            expression.accept(this);
        }
    }    

    @Override
    public void visit(Addition adtn) {
        if(!isRecognized(adtn)){
            visitBinaryOp(adtn);
        }
    }

    @Override
    public void visit(Division dvsn) {
        if(!isRecognized(dvsn)){
            visitBinaryOp(dvsn);
        }
    }

    @Override
    public void visit(Multiplication m) {
        if(!isRecognized(m)){
            visitBinaryOp(m);
        }
    }

    @Override
    public void visit(Subtraction s) {
        if(!isRecognized(s)){
            visitBinaryOp(s);
        }
    }
    
    private void visitBinaryOp(BinaryExpression be){
        be.getLeftExpression().accept(this);
        be.getRightExpression().accept(this);
    }
    
    @Override
    public void visit(Parenthesis prnths) {
        if(!isRecognized(prnths)){
            prnths.getExpression().accept(this);
        }
    }    
    
    private boolean isRecognized(Expression expr){
        if(isAllSubsMine(expr)){
            //if the same expression exists in inputTupleSchema, add it to the output schema
            _exprList.add(expr);
            return true;
        }else{
            return false;
        }
    }
    
    /*
     * This returns true if expr is availabe in inputTupleSchema, 
     *   or if all of its subexpressions are availabe in inputTupleSchema
     */
    private boolean isAllSubsMine(Expression expr){
        String strExpr = ParserUtil.getStringExpr(expr);
        if(_nt.contains(_inputTupleSchema, strExpr)){
            return true;
        }
        
        //unrecognized Column
        if(expr instanceof Column){
            return false;
        }
        
        //subExprs will be null if it's a constant
        List<Expression> subExprs = ParserUtil.getSubExpressions(expr);
        if(subExprs == null){
            return true;
        }
        
        //for all other we have to check
        for(Expression subExpr: subExprs){
            if (!isAllSubsMine(subExpr)){
                return false;
            }
        }
        
        //all of subexpressions are mine
        return true;
    }
    
    
    //not used
    @Override
    public void visit(NullValue nv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(InverseExpression ie) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(JdbcParameter jp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void visit(TimeValue tv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(TimestampValue tv) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(AndExpression ae) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(OrExpression oe) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Between btwn) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(EqualsTo et) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(GreaterThan gt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(GreaterThanEquals gte) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(InExpression ie) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(IsNullExpression ine) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(LikeExpression le) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(MinorThan mt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(MinorThanEquals mte) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(NotEqualsTo net) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(SubSelect ss) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(CaseExpression ce) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(WhenClause wc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(ExistsExpression ee) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(AllComparisonExpression ace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(AnyComparisonExpression ace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Concat concat) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Matches mtchs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(BitwiseAnd ba) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(BitwiseOr bo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(BitwiseXor bx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}